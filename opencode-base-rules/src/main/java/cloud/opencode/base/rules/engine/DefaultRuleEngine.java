package cloud.opencode.base.rules.engine;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.RuleEngine;
import cloud.opencode.base.rules.RuleResult;
import cloud.opencode.base.rules.conflict.ConflictResolver;
import cloud.opencode.base.rules.conflict.PriorityConflictResolver;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.RuleGroup;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Default Rule Engine Implementation
 * 默认规则引擎实现
 *
 * <p>Thread-safe implementation of the RuleEngine interface with support for
 * rule registration, conflict resolution, and execution listeners.</p>
 * <p>RuleEngine接口的线程安全实现，支持规则注册、冲突解决和执行监听器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concurrent rule registration - 并发规则注册</li>
 *   <li>Pluggable conflict resolution - 可插拔冲突解决</li>
 *   <li>Execution listeners - 执行监听器</li>
 *   <li>Multiple firing modes - 多种触发模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DefaultRuleEngine engine = new DefaultRuleEngine();
 * engine.register(rule1, rule2);
 * engine.setConflictResolver(PriorityConflictResolver.INSTANCE);
 * RuleResult result = engine.fire(context);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class DefaultRuleEngine implements RuleEngine {

    private static final Logger LOG = System.getLogger(DefaultRuleEngine.class.getName());

    private final Map<String, Rule> rules = new ConcurrentHashMap<>();
    private final Map<String, List<Rule>> groupIndex = new ConcurrentHashMap<>();
    private final List<RuleListener> listeners = new CopyOnWriteArrayList<>();
    private volatile ConflictResolver conflictResolver = PriorityConflictResolver.INSTANCE;

    @Override
    public RuleEngine register(Rule... rules) {
        for (Rule rule : rules) {
            this.rules.put(rule.getName(), rule);
            addToGroupIndex(rule);
        }
        return this;
    }

    @Override
    public RuleEngine register(RuleGroup group) {
        for (Rule rule : group.getRules()) {
            this.rules.put(rule.getName(), rule);
            addToGroupIndex(rule);
        }
        return this;
    }

    @Override
    public RuleEngine unregister(String ruleName) {
        Rule removed = rules.remove(ruleName);
        if (removed != null) {
            removeFromGroupIndex(removed);
        }
        return this;
    }

    @Override
    public RuleResult fire(RuleContext context) {
        return doFire(context, null, false, false);
    }

    @Override
    public RuleResult fire(RuleContext context, String group) {
        return doFire(context, group, false, false);
    }

    @Override
    public RuleResult fireFirst(RuleContext context) {
        return doFire(context, null, true, false);
    }

    @Override
    public RuleResult fireUntilHalt(RuleContext context) {
        return doFire(context, null, false, true);
    }

    private RuleResult doFire(RuleContext context, String group, boolean firstOnly, boolean untilHalt) {
        long startTime = System.nanoTime();
        RuleResult.Builder resultBuilder = RuleResult.successBuilder();

        notifyStart(context);

        try {
            List<Rule> applicableRules = getApplicableRules(group);
            List<Rule> orderedRules = conflictResolver.resolve(applicableRules);

            int maxIterations = untilHalt ? 1000 : 1;
            int iteration = 0;
            boolean anyFired;

            do {
                anyFired = false;
                iteration++;

                for (Rule rule : orderedRules) {
                    if (!rule.isEnabled()) {
                        resultBuilder.skipped(rule.getName());
                        continue;
                    }

                    try {
                        notifyBeforeEvaluate(rule, context);
                        boolean satisfied = rule.evaluate(context);
                        notifyAfterEvaluate(rule, context, satisfied);

                        if (satisfied) {
                            notifyBeforeExecute(rule, context);
                            rule.execute(context);
                            notifyAfterExecute(rule, context);

                            resultBuilder.fired(rule.getName());
                            anyFired = true;

                            if (firstOnly) {
                                break;
                            }
                        } else {
                            resultBuilder.skipped(rule.getName());
                        }
                    } catch (Exception e) {
                        notifyFailure(rule, context, e);
                        resultBuilder.failed(rule.getName(), e.getMessage(), e);
                    }
                }

                if (firstOnly && resultBuilder.build().hasFired()) {
                    break;
                }
            } while (untilHalt && anyFired && iteration < maxIterations);

            if (untilHalt && iteration >= maxIterations) {
                LOG.log(Level.WARNING,
                    "fireUntilHalt reached maximum iteration limit ({0}); rules may still be applicable",
                    maxIterations);
            }

            // Collect results from context
            resultBuilder.results(context.getResults());

        } finally {
            long elapsedNanos = System.nanoTime() - startTime;
            resultBuilder.executionTime(Duration.ofNanos(elapsedNanos));
        }

        RuleResult result = resultBuilder.build();
        notifyFinish(context, result.firedCount(), result.executionTime().toMillis());
        return result;
    }

    private List<Rule> getApplicableRules(String group) {
        if (group == null) {
            return new ArrayList<>(rules.values());
        }
        // O(1) group lookup via secondary index
        List<Rule> indexed = groupIndex.get(group);
        return indexed != null ? new ArrayList<>(indexed) : new ArrayList<>();
    }

    @Override
    public List<Rule> getRules() {
        return new ArrayList<>(rules.values());
    }

    @Override
    public List<Rule> getRules(String group) {
        return getApplicableRules(group);
    }

    @Override
    public Rule getRule(String name) {
        return rules.get(name);
    }

    @Override
    public boolean hasRule(String name) {
        return rules.containsKey(name);
    }

    @Override
    public int getRuleCount() {
        return rules.size();
    }

    @Override
    public RuleEngine addListener(RuleListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public RuleEngine removeListener(RuleListener listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public RuleEngine setConflictResolver(ConflictResolver resolver) {
        this.conflictResolver = resolver;
        return this;
    }

    @Override
    public void clear() {
        rules.clear();
        groupIndex.clear();
    }

    // Group index maintenance

    private void addToGroupIndex(Rule rule) {
        String group = rule.getGroup();
        if (group != null) {
            groupIndex.computeIfAbsent(group, _ -> new CopyOnWriteArrayList<>()).add(rule);
        }
    }

    private void removeFromGroupIndex(Rule rule) {
        String group = rule.getGroup();
        if (group != null) {
            groupIndex.computeIfPresent(group, (_, list) -> {
                list.remove(rule);
                return list.isEmpty() ? null : list;
            });
        }
    }

    // Listener notification methods

    private void notifyListeners(Consumer<RuleListener> action, String methodName) {
        for (RuleListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Listener {0} threw exception on {1}: {2}",
                    listener.getClass().getName(), methodName, e.getMessage());
            }
        }
    }

    private void notifyStart(RuleContext context) {
        notifyListeners(l -> l.onStart(context), "onStart");
    }

    private void notifyFinish(RuleContext context, int firedCount, long elapsedMillis) {
        notifyListeners(l -> l.onFinish(context, firedCount, elapsedMillis), "onFinish");
    }

    private void notifyBeforeEvaluate(Rule rule, RuleContext context) {
        notifyListeners(l -> l.beforeEvaluate(rule, context), "beforeEvaluate");
    }

    private void notifyAfterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
        notifyListeners(l -> l.afterEvaluate(rule, context, satisfied), "afterEvaluate");
    }

    private void notifyBeforeExecute(Rule rule, RuleContext context) {
        notifyListeners(l -> l.beforeExecute(rule, context), "beforeExecute");
    }

    private void notifyAfterExecute(Rule rule, RuleContext context) {
        notifyListeners(l -> l.afterExecute(rule, context), "afterExecute");
    }

    private void notifyFailure(Rule rule, RuleContext context, Exception exception) {
        notifyListeners(l -> l.onFailure(rule, context, exception), "onFailure");
    }
}
