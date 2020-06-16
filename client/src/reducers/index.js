import {combineReducers} from 'redux'
import AuthenticationReducer from "./AuthenticationReducer";
import ErrorReducer from "./ErrorReducer";
import FileReducer from "./FileReducer";
import NavigationReducer from "./NavigationReducer";
import UIReducer from "./UIReducer";
import UserReducer from "./UserReducer";

export default combineReducers({
    AuthenticationReducer, ErrorReducer, FileReducer, NavigationReducer, UIReducer, UserReducer
})