import "../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Button, Dimmer, Image, List, ListDescription, ListHeader, Loader} from 'semantic-ui-react'

import {
    attemptPreviewFile,
    attemptUploadFile,
    browseUp,
    clickFile,
    loadDirectory,
    loadRootDirectory,
    tryLogout
} from "../actions"

import {Redirect} from "react-router";
import {FILE_TYPES} from "../Constants";

class FileBrowser extends Component {

    componentDidMount() {
        if (this.props.files == null) {
            this.props.loadRootDirectory();
        }
    }

    chooseFile = () => {
        document.getElementById("file-picker").click();
    };

    render() {
        if (this.props.newFiles && this.props.newFiles.path === this.props.path) {
            console.log("Loading");
            this.props.loadDirectory(this.props.newFiles.path);
        }

        const items = this.props.files ? this.props.files.map((file, index) => {
            return {
                key: index,
                header: file.name,
                description: file.size ? "Size: " + file.size : "",
                imageSrc: file.thumbnail && file.thumbnail !== "" ? ("data:image/png;base64," + file.thumbnail) : null,
                onClick: e => this.props.clickFile(file),
                type: file.type,
                file: file
            }
        }).sort((a, b) => {
            if (a.type === FILE_TYPES.FOLDER && b.type !== FILE_TYPES.FOLDER) {
                return -1;
            } else if (a.type !== FILE_TYPES.FOLDER && b.type === FILE_TYPES.FOLDER) {
                return 1;
            } else {
                return a.name < b.name ? -1 : 1; // Assumes no duplicates.
            }
        }) : [];

        if (this.props.previewingFile) {
            return <Redirect to={'/preview'}/>
        }
        return this.props.isUserAuthenticated ? <div>
                <h1>Files</h1>
                {this.props.isLoading ?
                    <Dimmer active inverted>
                        <Loader inverted content={"Loading"}/>
                    </Dimmer>
                    :
                    <div>
                        <Button.Group>
                            <Button content={"Up"} onClick={this.props.browseUp.bind(this, this.props.path)}/>
                            <Button content={"Upload"} onClick={this.chooseFile}/>
                            <Button content={"Log Out"} onClick={this.props.tryLogout}/>
                        </Button.Group>
                        <input multiple type="file" className="file" id="file-picker" style={{"display": "none"}}
                               onChange={this.props.attemptUploadFile.bind(this, this.props.path)}/>
                        <List selection>
                            {items.map((i) => {
                                return <List.Item>
                                    {i.imageSrc ? <Image src={i.imageSrc}/> : null}
                                    <List.Content>
                                        <ListHeader><a onClick={i.onClick}>{i.header}</a></ListHeader>
                                        <ListDescription>{i.description}</ListDescription>
                                    </List.Content>
                                    {i.type === FILE_TYPES.FILE ?
                                        <List.Content floated={"right"}>
                                            <Button onClick={this.props.previewFile.bind(this, i.file)}>Preview</Button>
                                            <Button onClick={this.props.clickFile.bind(this, i.file)}>Download</Button>
                                        </List.Content> : null}
                                </List.Item>
                            })}
                        </List>
                    </div>
                }
            </div> :
            <Redirect to={"/login"}/>
    }
}


const mapStateToProps = state => ({
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    files: state.FileReducer.files,
    isLoading: state.FileReducer.isLoading,
    previewingFile: state.FileReducer.previewingFile,
    path: state.FileReducer.path,
    newFiles: state.FileReducer.newFiles
});

const mapDispatchToProps = dispatch => ({
    browseUp: path => dispatch(browseUp(path)),
    clickFile: file => dispatch(clickFile(file)),
    previewFile: file => dispatch(attemptPreviewFile(file)),
    loadRootDirectory: () => dispatch(loadRootDirectory()),
    loadDirectory: path => dispatch(loadDirectory(path)),
    attemptUploadFile: (path, e) => dispatch(attemptUploadFile(path, e)),
    tryLogout: () => dispatch(tryLogout())
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(FileBrowser)
