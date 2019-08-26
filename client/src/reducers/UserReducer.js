
import {ACTION} from "../Constants";
import userInitialState from "../state/UserInitialState"

export default (state = userInitialState, action) => {
    switch (action.type) {
        case ACTION.USER_CHANGED:
            return {
                ...state,
                username: action.payload.value
            };
        case ACTION.PASS_CHANGED:
            return {
                ...state,
                password: action.payload.value
            };
        case ACTION.AUTH_REJECTED_FROM_SERVER:
            return {
                ...state,
                isUserAuthenticated: false
            };
        case ACTION.AUTHENTICATED_WITH_SERVER:
            return {
                ...state,
                isUserAuthenticated: true
            };
        case ACTION.BEGIN_CHECK_AUTHENTICATION_WITH_SERVER:
            return {
                ...state,
                hasCheckedServerAuth: true
            }
    }
    return state;
};
