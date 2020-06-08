import {ACTION, NETWORK_FAIL_REASONS} from "../Constants";

export const handleError = (dispatch, error, defaultAction) => {
    if (error === NETWORK_FAIL_REASONS.AUTHENTICATION_MISSING) {
        // Reroute to login.
        dispatch({
            type: ACTION.AUTH_REJECTED_FROM_SERVER,
            payload: {
                error: error
            }
        });
    } else {
        dispatch({
            type: defaultAction,
            payload: {
                error: error
            }
        })
    }
};