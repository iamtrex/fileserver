import "../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {List} from 'semantic-ui-react'

import {clickFile, loadRootDirectory} from "../actions"
import {Redirect} from "react-router";

class FileBrowser extends Component {

    componentDidMount() {
        console.log("Tryign to load root directory!!!");
        this.props.loadRootDirectory();
    }

    render() {
        const items = this.props.files.map((file, index) => {
            return {
                key: index,
                content: file.name,
                description: file.size,
                onClick: e => this.props.clickFile(file)
            }
        });
        return this.props.isUserAuthenticated ? <div>
            <h1>Files</h1>
            <List selection items={items}/>
        </div> :
            <Redirect to={"/login"}/>
    }
}


const mapStateToProps = state => ({
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    files: state.FileReducer.files
});

const mapDispatchToProps = dispatch => ({
    clickFile: file => dispatch(clickFile(file)),
    loadRootDirectory: () => dispatch(loadRootDirectory())
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(FileBrowser)
