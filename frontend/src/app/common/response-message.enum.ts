export enum ResponseMessage {
    EXPIRED_TOKEN = 'response.message.expired_token',
    INVALID_TOKEN_TYPE = 'response.message.invalid_token_type',
    TOKEN_NOT_FOUND = 'response.message.token_not_found',
    PASSWORD_CONSTRAINT = 'response.message.password_constraint',
    PASSWORD_NOT_MATCH = 'response.message.password_not_match',
    REPEATED_PASSWORD = 'response.message.repeated_password',
    OPTIMISTIC_LOCK = 'response.message.optimistic_lock',
    CONSTRAINT_VIOLATION = 'response.message.constraint_violation',
    INACTIVE_ACCOUNT = 'response.message.inactive_account',
    ACCOUNT_NOT_FOUND = 'response.message.account_not_found',
    UNVERIFIED_ACCOUNT = 'response.message.unverified_account',
    AUTHENTICATION_EXCEPTION = 'response.message.authentication_exception',
    INVALID_PASSWORD = 'response.message.invalid_password',
    LANGUAGE_NOT_FOUND = 'response.message.language_not_found',
    LICENSE_NUMBER_ALREADY_TAKEN = 'response.message.license-number-already-taken',
    EMAIL_ADDRESS_ALREADY_TAKEN = 'response.message.email-address-already-taken',
    LOGIN_ALREADY_TAKEN = 'response.message.login-already-taken',
    ACCESS_LEVEL_NOT_FOUND = 'response.message.access_level_not_found',
    FORCE_PASSWORD_CHANGE_DATABASE_EXCEPTION = 'response.message.force_password_change_database_exception',
    ILLEGAL_SELF_ACTION = 'response.message.illegal_self_action',
    OVERRIDE_FORCED_PASSWORD_DATABASE_EXCEPTION = 'response.message.override_forced_password_database_exception',
    ACCESS_MANAGEMENT_SELF = 'response.message.access-management-self',
    SIGNATURE_MISMATCH = 'response.message.signature_mismatch',
    ROLLBACK_LIMIT_EXCEEDED = 'response.message.rollback.limit.exceeded',
    BAD_REQUEST = 'response.message.bad_request',
    CONFLICT = 'response.message.conflict',
    FORBIDDEN = 'response.message.forbidden',
    INTERNAL = 'response.message.internal',
    NOT_FOUND = 'response.message.not_found',
    UNAUTHORIZED = 'response.message.unauthorized',
    INVALID_UUID = 'response.message.invalid.uuid',
    BAD_ACCESS_LEVEL = 'response.message.bad-access-level',
    INVALID_CAPTCHA_CODE = 'response.message.invalid_captcha'
}
