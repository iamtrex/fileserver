import "../../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Button, Checkbox, Dimmer, List, Loader} from 'semantic-ui-react'

import {navigator} from "../Navigator";
import {FILE_TYPES, PAGES, VIEW_MODE} from "../../Constants";
import {FileListItem} from "../ListItem/FileListItem";
import {
    attemptDownloadFile, attemptLoadThumbnail,
    attemptPreviewFile,
    attemptUploadFile,
    browseUp,
    loadDirectory, toggleViewMode,
    tryLogout
} from "../../actions";
import {navigateTo} from "../../actions/NavigationActions";
import {FileThumbItem} from "../ListItem/FileThumbItem";
import CardColumns from "react-bootstrap/CardColumns";

class FileBrowser extends Component {

    componentDidMount() {
        if (this.props.files == null) { // TODO Might be scary? (Causing infinite loops)
            this.props.loadRootDirectory();
        }
    }

    chooseFile = () => {
        document.getElementById("file-picker").click();
    };

    clickFile = (file) => {
        if (file.type === FILE_TYPES.FOLDER) {
            this.props.loadDirectory(file.pathUrl);
        } else if (file.type === FILE_TYPES.FILE) {
            this.props.attemptDownloadFile(file);
        }
    };

    render() {
        let redirect = navigator(this.props.expectedPage, PAGES.FILE_BROWSER);
        if (redirect != null) {
            return redirect;
        }

        if (!this.props.isUserAuthenticated) {
            this.props.navigateTo(PAGES.LOGIN);
        }

        // If server has updated files, we should update them. // TODO Could this not be done automatically?
        if (this.props.serverUpdatedFiles && this.props.serverUpdatedFiles.path === this.props.path) {
            this.props.loadDirectory(this.props.serverUpdatedFiles.path);
        }

        const items = this.props.files ? this.props.files.map((file, index) => {
            return {
                key: index,
                header: file.name,
                description: file.size ? "Size: " + file.size : "",
                // TODO images can be loaded in client for more efficiency right?
                imageSrc: file.thumbnail && file.thumbnail !== "" ? ("data:image/png;base64," + file.thumbnail) : null,
                type: file.type,
                pathUrl: file.pathUrl,
                thumbnailSrc: file.thumbnailSrc,
                selectionOnClick: e => this.clickFile(file),
                previewOnClick: e => this.props.previewFile(file),
                downloadOnClick: e => this.clickFile(file)
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

        return <div>
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
                    <Checkbox toggle onChange={this.props.toggleViewMode} checked={this.props.viewMode === VIEW_MODE.THUMB}/>
                    <h1>{decodeURIComponent(this.props.path)}</h1>
                    <input multiple type="file" className="file" id="file-picker" style={{"display": "none"}}
                           onChange={this.props.attemptUploadFile.bind(this, this.props.path)}/>
                    {this.props.viewMode === VIEW_MODE.LIST ?
                        <List selection>
                            {items.map((i) => {
                                return <FileListItem i={i}/>
                            })}
                        </List> : null
                    }
                    {this.props.viewMode === VIEW_MODE.THUMB ?
                        <CardColumns>
                            {items.map((i) => {
                                return <FileThumbItem i={i} loadThumbnail={this.props.attemptLoadThumbnail.bind(this, i)}/>
                            })}
                        </CardColumns> : null
                    }


                </div>
            }
        </div>
    }
}


const mapStateToProps = state => ({
    expectedPage: state.NavigationReducer.expectedPage,
    isLoading: state.NavigationReducer.isLoading,
    isUserAuthenticated: state.AuthenticationReducer.isUserAuthenticated,
    files: state.FileReducer.files,
    previewingFile: state.FileReducer.previewingFile,
    path: state.FileReducer.path,
    serverUpdatedFiles: state.FileReducer.serverUpdatedFiles,
    viewMode: state.FileReducer.viewMode
});

const mapDispatchToProps = dispatch => ({
    navigateTo: (page) => dispatch(navigateTo(page)),
    browseUp: path => dispatch(browseUp(path)),
    previewFile: file => dispatch(attemptPreviewFile(file)),
    loadRootDirectory: () => dispatch(loadDirectory("/")),
    loadDirectory: path => dispatch(loadDirectory(path)),
    attemptUploadFile: (path, e) => dispatch(attemptUploadFile(path, e)),
    attemptDownloadFile: (file) => dispatch(attemptDownloadFile(file)),
    tryLogout: () => dispatch(tryLogout()),
    attemptLoadThumbnail: path => dispatch(attemptLoadThumbnail(path)),
    toggleViewMode: e => dispatch(toggleViewMode(e))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(FileBrowser)
