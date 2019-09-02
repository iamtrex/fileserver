import {ACTION, FILE_TYPES, VIEW_MODE} from "../Constants";

const INITIAL_STATE = Object.freeze({
    files: null,
    path: null,
    isUploading: false,
    serverUpdatedFiles: null,
    previewFile: null,
    viewMode: VIEW_MODE.LIST
});

export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.END_BROWSE_NEW_FILES_SUCCESS:
            return {
                ...state,
                files: action.payload.files,
                path: action.payload.path,
                serverUpdatedFiles: null
            };
        case ACTION.END_BROWSE_NEW_FILES_FAIL:
            return {
                ...state,
                serverUpdatedFiles: null
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
                isUploading: false,
                serverUpdatedFiles: {
                    path: action.payload.path
                }
            };
        case ACTION.END_UPLOAD_FILES_FAIL:
            return {
                ...state,
                isUploading: false,
                serverUpdatedFiles: null
            };
        case ACTION.AUTHENTICATION_MISSING:
        case ACTION.LOGOUT_SUCCESS:
            return {
                ...state,
                files: null,
                path: null,
                isUploading: false,
                previewFileType: null
            };
        case ACTION.LOAD_ICON:
            let files = [...state.files];
            files[action.payload.key].thumbnailSrc = action.payload.src;
            return {
                ...state,
                files: files
            }
        case ACTION.TOGGLE_VIEW_MODE:
            return {
                ...state,
                viewMode: state.viewMode === VIEW_MODE.LIST ? VIEW_MODE.THUMB : VIEW_MODE.LIST
            }
    }
    return state;
};
