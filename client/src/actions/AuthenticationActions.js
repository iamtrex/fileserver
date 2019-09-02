import {ACTION, INPUT_TYPES, PAGES} from "../Constants";
import {checkServerSession, login, logout, signup} from "../utils/RestClient";
import {SET_LOADING_FALSE_ACTION, SET_LOADING_TRUE_ACTION} from "./HelperActions";
import {navigateTo} from "./NavigationActions";

export const trySignup = (user, pass) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_SIGNUP
        });

        signup(user, pass).then(() => {
            dispatch({
                type: ACTION.SIGNUP_SUCCESS
            });

            login(user, pass).then(() => {
                dispatch({
                    type: ACTION.AUTHENTICATED_WITH_SERVER
                });
            }).catch((error) => {
                dispatch({
                    type: ACTION.AUTH_REJECTED_FROM_SERVER
                });
            });
        }).catch((error) => {
            dispatch({
                type: ACTION.SIGNUP_FAILURE
            });
        });
    }
};

export const tryLogin = (user, pass) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_LOGIN
        });

        login(user, pass).then(() => {
            dispatch({
                type: ACTION.AUTHENTICATED_WITH_SERVER
            });
            dispatch(navigateTo(PAGES.FILE_BROWSER));
        }).catch((error) => {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER
            });
        });
    }
};

export const tryLogout = () => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_LOGOUT
        });

        logout().then(() => {
            dispatch({
                type: ACTION.LOGOUT_SUCCESS
            });
            dispatch(navigateTo(PAGES.LOGIN));
        }).catch((error) => {
            dispatch({
                type: ACTION.LOGOUT_FAILED
            });
        });
    }
};

export const checkSession = () => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_CHECK_AUTHENTICATION_WITH_SERVER
        });
        dispatch(SET_LOADING_TRUE_ACTION);
        checkServerSession().then(() => {
            dispatch({
                type: ACTION.AUTHENTICATED_WITH_SERVER
            });
            dispatch(SET_LOADING_FALSE_ACTION);
            dispatch(navigateTo(PAGES.FILE_BROWSER));
        }).catch((error) => {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER
            });
            dispatch(SET_LOADING_FALSE_ACTION);
        });
    }
};

export const handleLoginInputChanged = (type, event) => {
    return {
        type: type === 'USER' ? ACTION.LOGIN_USER_CHANGED : ACTION.LOGIN_PASS_CHANGED,
        payload: {
            value: event.target.value
        }
    };
};

export const handleSignupInputChanged = (type, event) => {
    return {
        type: type === INPUT_TYPES.USER ? ACTION.SIGNUP_USER_CHANGED :
            type === INPUT_TYPES.PASSWORD ? ACTION.SIGNUP_PASS_CHANGED : ACTION.SIGNUP_CONFIRM_PASS_CHANGED,
        payload: {
            value: event.target.value
        }
    };
};