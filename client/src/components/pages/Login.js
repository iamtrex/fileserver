import "../../style/FileBrowser.less"
import React, {Component} from "react"

import {checkSession, handleLoginInputChanged, tryLogin} from "../../actions"
import {connect} from "react-redux"
import {Button, Form, Grid, Header, Image, Message, Segment} from 'semantic-ui-react'
import {navigator} from "../Navigator";
import {PAGES} from "../../Constants";
import {navigateTo} from "../../actions/NavigationActions";


class Login extends Component {
    componentDidMount() {
        if (!this.props.hasCheckedServerAuth) {
            this.props.checkSession();
        }
    }

    doLogin = () => {
        //TODO - Do some filtering or processing here?
        this.props.tryLogin(this.props.username, this.props.password);
    };

    render() {
        let redirect = navigator(this.props.expectedPage, PAGES.LOGIN);

        if (redirect != null) {
            console.log("Redirect was not null");
            return redirect;
        }

        if (this.props.isUserAuthenticated) {
            console.log("Hello");
            this.props.navigateTo(PAGES.FILE_BROWSER);
        }

        console.log("Main");
        return <Grid textAlign='center' style={{height: '100vh'}} verticalAlign='middle'>
            <Grid.Column style={{maxWidth: 450}}>
                <Header as='h2' color='teal' textAlign='center'>
                    <Image src='/logo512.png'/> Log-in to your account
                </Header>
                <Form size='large'>
                    <Segment stacked>
                        <Form.Input fluid icon='user' iconPosition='left' placeholder='Username'
                                    value={this.props.username}
                                    onChange={this.props.handleLoginInputChanged.bind(this, 'USER')}/>
                        <Form.Input
                            fluid
                            icon='lock'
                            iconPosition='left'
                            placeholder='Password'
                            type='password'
                            value={this.props.password}
                            onChange={this.props.handleLoginInputChanged.bind(this, 'PASSWORD')}
                        />

                        <Button color='teal' fluid size='large' onClick={this.doLogin.bind(this)}>
                            Login
                        </Button>
                    </Segment>
                </Form>
                <Message>
                    New to us? <Button onClick={this.props.navigateTo.bind(this, PAGES.SIGNUP)} size="mini"
                                       content={"Sign Up!"}/>
                </Message>
            </Grid.Column>
        </Grid>
    }
}


const mapStateToProps = state => ({
    expectedPage: state.NavigationReducer.expectedPage,
    username: state.AuthenticationReducer.loginUsername,
    password: state.AuthenticationReducer.loginPassword,
    isUserAuthenticated: state.AuthenticationReducer.isUserAuthenticated,
    hasCheckedServerAuth: state.AuthenticationReducer.hasCheckedServerAuth
});

const mapDispatchToProps = dispatch => ({
    navigateTo: (page) => dispatch(navigateTo(page)),
    checkSession: () => dispatch(checkSession()),
    tryLogin: (user, pass) => dispatch(tryLogin(user, pass)),
    handleLoginInputChanged: (type, event) => dispatch(handleLoginInputChanged(type, event))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(Login)
