import React from 'react';
import {BrowserRouter as Router, Route,} from 'react-router-dom'

import FileBrowser from "./FileBrowser";
import Login from "./Login";
import Signup from "./Signup";

import 'semantic-ui-css/semantic.min.css';


const App = () => (
    <Router>
        <div className={"router-div"}>
            <Route exact path={"/"} render={() => <Login/>}/>
            <Route path={"/login"} render={() => <Login/>}/>
            <Route path={"/files"} render={() => <FileBrowser/>}/>
            <Route path={"/signup"} render={() => <Signup/>}/>
        </div>
    </Router>
);

export default App;