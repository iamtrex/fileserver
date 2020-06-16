import {ACTION} from "../Constants";

export const showCreateFolderDialog = (show) => {
    return {
        type: ACTION.SHOW_CREATE_FOLDER_DIALOG,
        payload: {
            show: show
        }
    }
};

export const showShareFileDialog = (show) => {
    return {
        type: ACTION.SHOW_SHARE_FILE_DIALOG,
        payload: {
            show: show
        }
    }
};