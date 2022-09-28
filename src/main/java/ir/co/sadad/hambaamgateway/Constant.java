package ir.co.sadad.hambaamgateway;

public interface Constant {

    String AUTHORIZATION_REGEX = "^Bearer\\s([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_\\-\\+\\/=]*)$";

    String SCOPE = "scope";
    String SPACE = " ";
    String EMPTY_STRING = "";
    String BEARER__PREFIX = "Bearer ";
    String CHECK_VERSION_PATH = "/check-version-api/resources/version/history/**";
    String REMOTE_CONFIG_PATH = "/remote-config/";

}
