import {ACTION} from "../Constants";

const USER_INITIAL_STATE = Object.freeze({
    "loginUsername": "",
    "loginPassword": "",
    "signupUsername": "",
    "signupPassword": "",
    "signupConfirmPassword": "",
    "isUserAuthenticated": false,
    "hasCheckedServerAuth": false
});

export default (state = USER_INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.SIGNUP_USER_CHANGED:
            return {
                ...state,
                signupUsername: action.payload.value.toLowerCase() //TODO remove this.
            };
        case ACTION.SIGNUP_PASS_CHANGED:
            return {
                ...state,
                signupPassword: action.payload.value
            };
        case ACTION.SIGNUP_CONFIRM_PASS_CHANGED:
            return {
                ...state,
                signupConfirmPassword: action.payload.value
            };
        case ACTION.LOGIN_USER_CHANGED:
            return {
                ...state,
                loginUsername: action.payload.value.toLowerCase()
            };
        case ACTION.LOGIN_PASS_CHANGED:
            return {
                ...state,
                loginPassword: action.payload.value
            };
        case ACTION.AUTH_REJECTED_FROM_SERVER:
            return {
                ...state,
                isUserAuthenticated: false,
                hasCheckedServerAuth: true,
                loginUsername: "",
                loginPassword: ""
            };
        case ACTION.AUTHENTICATED_WITH_SERVER:
            return {
                ...state,
                isUserAuthenticated: true,
                loginUsername: "",
                loginPassword: ""
            };
        case ACTION.BEGIN_CHECK_AUTHENTICATION_WITH_SERVER:
            return {
                ...state,
                hasCheckedServerAuth: true
            };
        case ACTION.LOGOUT_FAILED:
            return {
                ...state,
                hasCheckedServerAuth: false
            };
        case ACTION.LOGOUT_SUCCESS:
            return {
                ...state,
                isUserAuthenticated: false,
                hasCheckedServerAuth: true,
            };
        case ACTION.SIGNUP_FAILURE:
        case ACTION.SIGNUP_SUCCESS:
            return {
                ...state,
                signupUsername: "",
                signupPassword: "",
                signupConfirmPassword: ""
            }
    }
    return state;
};
