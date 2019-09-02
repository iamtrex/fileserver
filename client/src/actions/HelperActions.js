import {ACTION} from "../Constants";

export const SET_LOADING_TRUE_ACTION = Object.freeze({
    type: ACTION.SET_IS_SYNCHRONOUS_LOADING,
    payload: {
        isLoading: true
    }
});

export const SET_LOADING_FALSE_ACTION = Object.freeze({
    type: ACTION.SET_IS_SYNCHRONOUS_LOADING,
    payload: {
        isLoading: false
    }
});