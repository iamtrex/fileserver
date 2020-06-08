import styles from "../style/Previewer.less";

import React, {Component} from "react"

import {connect} from "react-redux"
import {Button, Dimmer, Loader} from 'semantic-ui-react'
import {FILE_TYPES, PAGES} from "../Constants";
import {ImagePreviewer} from "../components/previewers/ImagePreviewer";
import {VideoPreviewer} from "../components/previewers/VideoPreviewer";
import {navigator} from "./Navigator";
import {navigateTo} from "../actions/NavigationActions";

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
                <div className={styles.shell}>
                    <Button onClick={this.props.navigateTo.bind(this, PAGES.FILE_BROWSER)}>Back</Button>
                </div>
                <div className={styles.wrapper}>
                    {this.props.previewFile.type === FILE_TYPES.IMAGE ?
                        <ImagePreviewer src={this.props.previewFile.src}/>
                        : null}
                    {this.props.previewFile.type === FILE_TYPES.VIDEO ?
                        <VideoPreviewer src={this.props.previewFile.src}
                                        type={this.props.previewFile.videoType}/> : null}
                </div>
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
