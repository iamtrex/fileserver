import React, {Component} from "react"
import Modal from "react-bootstrap/Modal";
import {shareFiles, showShareFileDialog} from "../../actions";
import {connect} from "react-redux";

class CreateFolderDialog extends Component {
    constructor(props) {
        super(props);
        this.inputRef = React.createRef();
    }

    render() {
        return (
            <>
                <Modal centered aria-labelledby="contained-modal-title-vcenter"
                       show={this.props.isShow}
                       onHide={this.props.showShareFileDialog.bind(this, false)}>
                </Modal>
            </>
        );
    }

};

const mapStateToProps = state => ({
    isShow: state.UIReducer.isShareFileDialogShowing,
    users: state.UserReducer.users,
    isLoadingUsers: state.UserReducer.isLoadingUsers
});

const mapDispatchToProps = dispatch => ({
    shareFiles: () => dispatch(shareFiles()),
    showShareFileDialog: (show) => dispatch(showShareFileDialog(show))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(CreateFolderDialog)
