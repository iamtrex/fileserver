import {combineReducers} from 'redux'
import FileReducer from "./FileReducer";
import UserReducer from "./UserReducer";

export default combineReducers({
    FileReducer, UserReducer
})