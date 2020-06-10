import style from "../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Dimmer, List, Loader} from 'semantic-ui-react'

import {navigator} from "./Navigator";
import {FILE_TYPES, PAGES, VIEW_MODE} from "../Constants";
import {FileListItem} from "../components/listItems/FileListItem";
import {
    attemptDownloadFile,
    attemptLoadThumbnail,
    attemptPreviewFile,
    attemptUploadFile,
    browseUp,
    loadDirectory,
    showCreateFolderDialog,
    toggleViewMode,
    tryLogout
} from "../actions";
import {navigateTo} from "../actions/NavigationActions";
import {FileThumbItem} from "../components/listItems/FileThumbItem";
import CardColumns from "react-bootstrap/CardColumns";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import Button from "react-bootstrap/Button";
import CreateFolderDialog from "../components/dialogs/CreateFolderDialog";
import CardDeck from "react-bootstrap/CardDeck";

class FileBrowser extends Component {

    componentDidMount() {
        // TODO might be causing double load.
        if (this.props.files === null) { // TODO Might be scary? (Causing infinite loops)
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
            this.props.previewFile(file);
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
                downloadOnClick: e => this.props.attemptDownloadFile(file)
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

        return <div className={style.fileRoot}>
            <CreateFolderDialog/>
            <h1>Files</h1>
            {this.props.isLoading ?
                <Dimmer active inverted>
                    <Loader inverted content={"Loading"}/>
                </Dimmer>
                :
                <div>
                    <ButtonGroup>
                        <Button variant={"secondary"}
                                onClick={this.props.browseUp.bind(this, this.props.path)}>Up</Button>
                        <Button variant={"secondary"} onClick={this.chooseFile}>Upload</Button>
                        <Button variant={"secondary"}
                                onClick={this.props.showCreateFolderDialog.bind(this, true)}>
                            New Folder
                        </Button>
                        <Button variant={"secondary"} onClick={this.props.tryLogout}>Log Out</Button>
                    </ButtonGroup>
                    <hr/>
                    <ButtonGroup toggle>
                        <Button defaultChecked variant={"secondary"}
                                onClick={this.props.toggleViewMode.bind(this, VIEW_MODE.LIST)}>List View</Button>
                        <Button variant={"secondary"} onClick={this.props.toggleViewMode.bind(this, VIEW_MODE.THUMB)}>Thumbnail
                            View</Button>
                    </ButtonGroup>
                    <h2>{decodeURIComponent(this.props.path)}</h2>
                    <h2>{this.props.files && this.props.files.length ? (this.props.files.length + " files") : "No files found"}</h2>
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
                        <CardDeck className={style.cardColumns}>
                            {items.map((i) => {
                                return <FileThumbItem i={i}
                                                      loadThumbnail={this.props.attemptLoadThumbnail.bind(this, i)}/>
                            })}
                        </CardDeck> : null
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
    toggleViewMode: viewMode => dispatch(toggleViewMode(viewMode)),
    showCreateFolderDialog: (show) => dispatch(showCreateFolderDialog(show))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(FileBrowser)
