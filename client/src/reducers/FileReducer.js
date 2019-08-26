import initialState from "../state/InitialState"
import {ACTION} from "../Constants";

export default (state = initialState, action) => {
    switch (action.type) {
        case ACTION.BEGIN_BROWSE_NEW_FILES:
            return {
                ...state,
            };
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
            return {
                ...state,
                files: action.payload.files
            };
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
            return {
                ...state,
            };

    }
    return state;
};
