

/**
 * OpenCode Base Captcha Module
 * OpenCode Base 验证码模块
 *
 * <p>Lightweight CAPTCHA generation library with support for multiple types.</p>
 * <p>轻量级验证码生成库，支持多种类型。</p>
 */
module cloud.opencode.base.captcha {

    requires java.desktop;
    requires cloud.opencode.base.core;

    exports cloud.opencode.base.captcha;
    exports cloud.opencode.base.captcha.exception;
    exports cloud.opencode.base.captcha.generator;
    exports cloud.opencode.base.captcha.interactive;
    exports cloud.opencode.base.captcha.renderer;
    exports cloud.opencode.base.captcha.store;
    exports cloud.opencode.base.captcha.validator;
    exports cloud.opencode.base.captcha.codec;
    exports cloud.opencode.base.captcha.security;
    exports cloud.opencode.base.captcha.support;
}
