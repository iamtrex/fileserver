import "../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Button, Dimmer, List, Loader} from 'semantic-ui-react'

import {browseUp, clickFile, loadRootDirectory} from "../actions"
import {Redirect} from "react-router";

class FileBrowser extends Component {

    componentDidMount() {
        console.log("Trying to load root directory!!!");
        this.props.loadRootDirectory();
    }

    render() {
        const items = this.props.files.map((file, index) => {
            return {
                key: index,
                content: {
                    header: file.name,
                    description: file.size ? "Size: " + file.size : ""
                },
                image: {
                    src: "data:image/png;base64," + file.thumbnail
                },
                onClick: e => this.props.clickFile(file)

            }
        });
        return this.props.isUserAuthenticated ? <div>
                <h1>Files</h1>
                {this.props.isLoading ?
                    <Dimmer active inverted>
                        <Loader inverted content={"Loading"}/>
                    </Dimmer>
                    :
                    <div>
                        <Button
                            content={"Up"}
                            onClick={this.props.browseUp}
                        />
                        <h3>{items.length} files</h3>
                        <List selection items={items}/></div>
                }
            </div> :
            <Redirect to={"/login"}/>
    }
}


const mapStateToProps = state => ({
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    files: state.FileReducer.files,
    isLoading: state.FileReducer.isLoading
});

const mapDispatchToProps = dispatch => ({
    browseUp: () => dispatch(browseUp()),
    clickFile: file => dispatch(clickFile(file)),
    loadRootDirectory: () => dispatch(loadRootDirectory())
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(FileBrowser)
