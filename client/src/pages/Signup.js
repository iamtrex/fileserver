import "../style/FileBrowser.less"
import React, {Component} from "react"

import {checkSession, handleSignupInputChanged, trySignup} from "../actions"
import {connect} from "react-redux"
import {Button, Form, Grid, Header, Image, Segment} from 'semantic-ui-react'
import {navigator} from "./Navigator";
import {INPUT_TYPES, PAGES} from "../Constants";
import {navigateTo} from "../actions/NavigationActions";


class Signup extends Component {

    componentDidMount() {
        if (!this.props.hasCheckedServerAuth) {
            this.props.checkSession();
        }
    }

    doSignup = () => {
        if (this.props.password !== this.props.confirmPassword) {
            // Throw error to UI.
        } else {
            this.props.trySignup(this.props.username, this.props.password);
        }
    };

    render() {
        let redirect = navigator(this.props.expectedPage, PAGES.SIGNUP);
        if (redirect != null) {
            return redirect;
        }

        if (this.props.isUserAuthenticated) {
            navigateTo(PAGES.FILE_BROWSER);
        }

        return <Grid textAlign='center' style={{height: '100vh'}} verticalAlign='middle'>
            <Grid.Column style={{maxWidth: 450}}>
                <Header as='h2' color='teal' textAlign='center'>
                    <Image src='/logo.png'/> Log-in to your account
                </Header>
                <Form size='large'>
                    <Segment stacked>
                        <Form.Input
                            fluid icon='user'
                            iconPosition='left'
                            placeholder='Username'
                            value={this.props.username}
                            onChange={this.props.handleSignupInputChanged.bind(this, INPUT_TYPES.USER)}/>

                        <Form.Input
                            fluid
                            icon='lock'
                            iconPosition='left'
                            placeholder='Password'
                            type='password'
                            value={this.props.password}
                            onChange={this.props.handleSignupInputChanged.bind(this, INPUT_TYPES.PASSWORD)}
                        />

                        <Form.Input
                            fluid
                            icon='lock'
                            iconPosition='left'
                            placeholder='Confirm Password'
                            type='password'
                            value={this.props.confirmPassword}
                            onChange={this.props.handleSignupInputChanged.bind(this, INPUT_TYPES.CONFIRM_PASSWORD)}
                        />

                        <Button color='teal' fluid size='large' onClick={this.doSignup.bind(this)}>
                            Sign Up!
                        </Button>
                    </Segment>
                </Form>
            </Grid.Column>
        </Grid>
    }
}


const mapStateToProps = state => ({
    expectedPage: state.NavigationReducer.expectedPage,
    username: state.AuthenticationReducer.signupUsername,
    password: state.AuthenticationReducer.signupPassword,
    confirmPassword: state.AuthenticationReducer.signupConfirmPassword,
    isUserAuthenticated: state.AuthenticationReducer.isUserAuthenticated,
    hasCheckedServerAuth: state.AuthenticationReducer.hasCheckedServerAuth
});

const mapDispatchToProps = dispatch => ({
    navigateTo: (page) => dispatch(navigateTo(page)),
    checkSession: () => dispatch(checkSession()),
    trySignup: (user, pass) => dispatch(trySignup(user, pass)),
    handleSignupInputChanged: (type, event) => dispatch(handleSignupInputChanged(type, event))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Signup)
