import React, { Component } from 'react';
import {StyleSheet, Text, TextInput, View, Image, Alert} from 'react-native';

const linestyle = {height:6, marginTop: 4, marginBottom: 4, overflow: 'hidden'}

const Background = ({width, opacity}) => {return (
  <View style={{flexDirection:'row', width: width, opacity: opacity, top: 0,
    bottom: 0, left: 0, right: 0, position: 'absolute'}}>
      <View style={{width: '85%', backgroundColor: 'green'}}></View>
      <View style={{width: '10%', backgroundColor: 'yellow'}}></View>
      <View style={{width: '5%', backgroundColor: 'red'}}></View>
    </View>
)};

class MonitorLine extends Component {
  constructor(props){
    super(props);
    this.state = {};
    this.setWidth = this.setWidth.bind(this);
  }
  setWidth(w){
    this.ref && this.ref.setNativeProps({style: {...linestyle, width: w + "%"}})
  }
  render(){
    return (<View
              onLayout={(e)=>{this.setState({width:e.nativeEvent.layout.width})}}
              style={{backgroundColor: 'rgba(0,0,0,0.1)'}}>
      <Background opacity={0.1}/>
      {this.state && this.props.inProgress ?
       <View ref={(e)=>this.ref=e} style={linestyle}>
         <Background opacity={1} width={this.state.width}/>
       </View>
       :
       <View style={{height:14}}/>
      }
    </View>)
  }
}

export default class Monitor extends Component {
  constructor(props){
    super(props);
    this.state = {};
    this.setWidth = this.setWidth.bind(this);
  }
  setWidth(w){
    this.line1 && this.line1.setWidth(w);
    this.line2 && this.line2.setWidth(w);
  }
  render(){
    return (
      <View style={{backgroundColor:"#aaa"}}>
        <MonitorLine inProgress={this.props.inProgress} ref={(e)=>{this.line1=e}} />
        <View style={{height:4}}/>
        <MonitorLine inProgress={this.props.inProgress} ref={(e)=>this.line2=e} />
      </View>)
  }
}

const styles = StyleSheet.create({
  container: {
    paddingTop: 24,
    flex: 1,
    backgroundColor: 'white'
  },
});
