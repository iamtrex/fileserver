import initialState from "../state/InitialState"
import {ACTION} from "../Constants";

export default (state = initialState, action) => {
    switch (action.type) {
        case ACTION.BEGIN_BROWSE_NEW_FILES:
        case ACTION.BEGIN_DOWNLOAD_FILE:
            return {
                ...state,
                isLoading: true
            };
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
            return {
                ...state,
                files: action.payload.files,
                path: action.payload.path,
                isLoading: false
            };
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
            return {
                ...state,
                isLoading: false
            };
        case ACTION.END_DOWNLOAD_FILE_FAIL:
        case ACTION.END_DOWNLOAD_FILE_SUCCESS:
            return {
                ...state,
                isLoading: false
            };
    }
    return state;
};
