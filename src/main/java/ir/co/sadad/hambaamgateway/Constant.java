package ir.co.sadad.hambaamgateway;

public interface Constant {

    String AUTHORIZATION_REGEX = "^Bearer\\s([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_=]+)\\.([a-zA-Z0-9_\\-\\+\\/=]*)$";

    String SCOPE = "scope";
    String SPACE = " ";
    String EMPTY_STRING = "";
    String BEARER__PREFIX = "Bearer ";
    String CAR_TOLL_PATH = "/car-toll-api/";

    String AVATAR_API_PATH = "/avatar-api/";
    String REMOTE_CONFIG_PATH = "/remote-config/";

}
