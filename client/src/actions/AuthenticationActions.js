import {ACTION} from "../Constants";
import {checkServerSession, login, logout, signup} from "../utils/RestClient";

export const trySignup = (user, pass) => {
    return (dispatch) => {
        dispatch({
            type: ACTION.BEGIN_SIGNUP
        });

        signup(user, pass).then(() => {
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

        checkServerSession().then(() => {
            dispatch({
                type: ACTION.AUTHENTICATED_WITH_SERVER
            });
        }).catch((error) => {
            dispatch({
                type: ACTION.AUTH_REJECTED_FROM_SERVER
            })
        });
    }
};

export const handleUserChanged = (type, event) => {
    return {
        type: type === 'USER' ? ACTION.USER_CHANGED : ACTION.PASS_CHANGED,
        payload: {
            value: event.target.value
        }
    };
};

export const handleSignupUserChanged = (type, event) => {
    return {
        type: type === 'USER' ? ACTION.SIGNUP_USER_CHANGED :
            type === 'PASSWORD' ? ACTION.SIGNUP_PASS_CHANGED : ACTION.SIGNUP_CONFIRM_PASS_CHANGED,
        payload: {
            value: event.target.value
        }
    };
};