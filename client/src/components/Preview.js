import "../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Button} from 'semantic-ui-react'

import {Redirect} from "react-router";
import {FILE_TYPES} from "../Constants";
import {ImagePreviewer} from "./ImagePreviewer";
import {stopPreviewing} from "../actions";
import {VideoPreviewer} from "./VideoPreviewer";

class Preview extends Component {
    render() {
        if (!this.props.previewingFile) {
            return <Redirect to={'/files'}/>
        }

        return this.props.isUserAuthenticated ?
            <div>
                <span><Button onClick={this.props.stopPreviewing}>Back</Button></span>
                <hr/>
                {this.props.previewFileType === FILE_TYPES.IMAGE ? <ImagePreviewer src={this.props.imageSrc}/> : null}
                {this.props.previewFileType === FILE_TYPES.VIDEO ? <VideoPreviewer src={this.props.videoSrc} type={this.props.videoType}/> : null}
            </div> :
            <Redirect to={"/login"}/>
    }
}


const mapStateToProps = state => ({
    imageSrc: state.FileReducer.imageSrc,
    videoSrc: state.FileReducer.videoSrc,
    videoType: state.FileReducer.videoType,
    previewFileType: state.FileReducer.previewFileType,
    isUserAuthenticated: state.UserReducer.isUserAuthenticated,
    previewingFile: state.FileReducer.previewingFile
});

const mapDispatchToProps = dispatch => ({
    stopPreviewing: () => dispatch(stopPreviewing())
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Preview)
