import React from 'react';
import {BrowserRouter as Router, Route,} from 'react-router-dom'

import FileBrowser from "./pages/FileBrowser";
import Login from "./pages/Login";
import Preview from "./pages/Preview";
import Signup from "./pages/Signup";

import 'semantic-ui-css/semantic.min.css';


const App = () => (
    <Router>
        <div className={"router-div"}>
            <Route exact path={"/"} render={() => <Login/>}/>
            <Route path={"/login"} render={() => <Login/>}/>
            <Route path={"/files"} render={() => <FileBrowser/>}/>
            <Route path={"/signup"} render={() => <Signup/>}/>
            <Route path={"/preview"} render={() => <Preview/>}/>
        </div>
    </Router>
);

export default App;