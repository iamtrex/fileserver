import {ACTION, FILE_TYPES, VIEW_MODE} from "../Constants";

const INITIAL_STATE = Object.freeze({
    files: null,
    path: null,
    isUploading: false,
    previewFile: null,
    viewMode: VIEW_MODE.LIST,
    showCreateFolderDialog: false,
    isCreateFolderNameValid: false,
    isCreateFolderNameInvalid: false
});

export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.CREATE_FOLDER_NAME_CHANGE:
            return {
                ...state,
                isCreateFolderNameValid: action.payload.isValid,
                isCreateFolderNameInvalid: !action.payload.isValid
            };
        case ACTION.SHOW_CREATE_FOLDER_DIALOG:
            return {
                ...state,
                isCreateFolderDialogShowing: action.payload.show
            };
        case ACTION.END_CREATE_FOLDER_SUCCESS:
            return {
                ...state,
                isCreateFolderDialogShowing: false
            };
        case ACTION.END_CREATE_FOLDER_FAIL:
            return {
                ...state,
                isCreateFolderDialogShowing: false
            };
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
            return {
                ...state,
                files: action.payload.files,
                path: action.payload.path
            };
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
            return {
                ...state,
                files: []
            };
        case ACTION.END_PREVIEW_FILE_SUCCESS:
            if (action.payload.type === FILE_TYPES.IMAGE) {
                return {
                    ...state,
                    isLoading: false,
                    previewFile: {
                        type: action.payload.type,
                        src: action.payload.src
                    }
                };
            } else if (action.payload.type === FILE_TYPES.VIDEO) {
                return {
                    ...state,
                    isLoading: false,
                    previewFile: {
                        type: action.payload.type,
                        src: action.payload.src,
                        videoType: action.payload.videoType
                    }
                };
            }
            console.log("FAIL SHOULD NOT HAPPEN. - FILE TYPE NOT RECOGNIZED.");
            return {
                ...state
            };
        case ACTION.BEGIN_UPLOAD_FILES:
            return {
                ...state,
                isUploading: true
            };
        case ACTION.END_UPLOAD_FILES:
            return {
                ...state,
                isUploading: false
            };
        case ACTION.END_UPLOAD_FILES_FAIL:
            return {
                ...state,
                isUploading: false
            };
        case ACTION.AUTHENTICATION_MISSING:
        case ACTION.LOGOUT_SUCCESS:
            return INITIAL_STATE;
        case ACTION.LOAD_ICON:
            let files = [...state.files];
            // If file still exists in our view - set the thumbnail.
            let file = files[action.payload.key];
            if (file) {
                file.thumbnailSrc = action.payload.src;
            }
            return {
                ...state,
                files: files
            };
        case ACTION.TOGGLE_VIEW_MODE:
            return {
                ...state,
                viewMode: action.payload.viewMode
            };
    }
    return state;
};
