import "../../style/FileBrowser.less"
import React, {Component} from "react"

import {connect} from "react-redux"
import {Button, Dimmer, Loader} from 'semantic-ui-react'
import {FILE_TYPES, PAGES} from "../../Constants";
import {ImagePreviewer} from "../previewers/ImagePreviewer";
import {VideoPreviewer} from "../previewers/VideoPreviewer";
import {navigator} from "../Navigator";
import {navigateTo} from "../../actions/NavigationActions";

class Preview extends Component {
    render() {
        let redirect = navigator(this.props.expectedPage, PAGES.PREVIEWER);
        if (redirect != null) {
            return redirect;
        }
        if (!this.props.isUserAuthenticated) {
            this.props.navigateTo(PAGES.LOGIN);
        }

        if (!this.props.previewFile) {
            this.props.navigateTo(PAGES.FILE_BROWSER);
        }

        return this.props.isLoading ?
            <Dimmer active inverted>
                <Loader inverted content={"Loading"}/>
            </Dimmer>
            :
            <div>
                <Button onClick={this.props.navigateTo.bind(this, PAGES.FILE_BROWSER)}>Back</Button>
                <hr/>
                {this.props.previewFile.type === FILE_TYPES.IMAGE ?
                    <ImagePreviewer src={this.props.previewFile.src}/> : null}
                {this.props.previewFile.type === FILE_TYPES.VIDEO ?
                    <VideoPreviewer src={this.props.previewFile.src}
                                    type={this.props.previewFile.videoType}/> : null}
            </div>
    }
}


const mapStateToProps = state => ({
    expectedPage: state.NavigationReducer.expectedPage,
    previewFile: state.FileReducer.previewFile,
    isUserAuthenticated: state.AuthenticationReducer.isUserAuthenticated
});

const mapDispatchToProps = dispatch => ({
    navigateTo: (page) => dispatch(navigateTo(page))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Preview)
