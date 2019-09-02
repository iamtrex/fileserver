import {ACTION} from "../Constants";

export default (state = {}, action) => {
    switch (action.type) {
        case ACTION.BEGIN_BROWSE_NEW_FILES:
        case ACTION.BEGIN_DOWNLOAD_FILE:
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
        case ACTION.END_DOWNLOAD_FILE_FAIL:
        case ACTION.END_DOWNLOAD_FILE_SUCCESS:
        case ACTION.BEGIN_PREVIEW_FILE:
        case ACTION.END_PREVIEW_FILE_SUCCESS:
        case ACTION.FILE_NOT_PREVIEWABLE:
        case ACTION.STOP_PREVIEW:
        case ACTION.BEGIN_UPLOAD_FILES:
        case ACTION.END_UPLOAD_FILES:
        case ACTION.END_UPLOAD_FILES_FAIL:
        case ACTION.AUTHENTICATION_MISSING:
        case ACTION.LOGOUT_SUCCESS:
            return state;
    }
    return state;
};
