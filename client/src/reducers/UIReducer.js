import {ACTION} from "../Constants";

const INITIAL_STATE = Object.freeze({
    isCreateFolderDialogShowing: false,
    isShareFileDialogShowing: false
});

export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case ACTION.SHOW_CREATE_FOLDER_DIALOG:
            return {
                ...state,
                isCreateFolderDialogShowing: action.payload.show
            };

        case ACTION.SHOW_SHARE_FILE_DIALOG:
            return {
                ...state,
                isShareFileDialogShowing: action.payload.show
            };

        default:
            return state;
    }
    ;
}