import {ACTION,} from "../Constants";

const INITIAL_STATE = Object.freeze({
    users: null,
    isLoadingUsers: false
});

export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.START_UPDATE_SHARE:
            return {
                ...state,
                isLoadingUsers: true
            };
        case ACTION.END_UPDATE_SHARE_SUCCESS:
            return {
                ...state,
                users: action.payload.users,
                isLoadingUsers: false
            };
        case ACTION.END_UPDATE_SHARE_FAIL:
            // TODO what to do with users on fail?
            return {
                ...state,
                isLoadingUsers: false
            };
    }
    return state;
};
