import React, { Component } from 'react';
import {StyleSheet, Text, TextInput, View, Image, Alert} from 'react-native';

/* (defn monitor-line [id]
 *   (let [monitor-value (subscribe [::model/monitoring])
 *         in-progress? (subscribe [::model/recording])
 *         top-w        (atom 0)
 *         line-bg      (fn [o]
 *                        [view {:style [st/box st/row (st/opacity o) (st/width @top-w)]}
 *                         [view {:style [(st/width "85%") (st/background "green")]}]
 *                         [view {:style [(st/width "10%") (st/background "yellow")]}]
 *                         [view {:style [(st/width "5%") (st/background "red")]}]])]
 *     (fn []
 *       [view {:on-layout (fn [e] (let [w (-> e .-nativeEvent .-layout .-width)]
 *                                   (reset! top-w w)))
 *              :style     [(st/gray 1)]}
 *        [line-bg 0.1]
 *        (if @in-progress?
 *          [view {:ref   #(swap! monitor-lines-refs assoc id %)
 *                 :style [(st/height 6)
 *                         (st/margin 4 0)
 *                         (st/width @monitor-value)
 *                         (st/overflow "hidden")]}
 *           [line-bg 1]]
 *          [view {:style [(st/height 14)]}])])))*/

const linestyle = {height:6, marginTop: 6, marginBottom: 6, overflow: 'hidden'}

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
