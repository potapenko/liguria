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

import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  TextInput,
  View,
  Image,
  Linking,
} from 'react-native';

const Line = ({topWidth}) => {(
  (<View>


  </View>)

)};

const linestyle = {};

class MonitorLine extends Component {
  setWidth(){
  }
  render(){
    return (<View>
      ref={(e)=>this.ref = e}
      style={linestyle}
      <Text>Line</Text>

    </View>)
  }
}

export default class Monitor extends Component {
  render(){
    return (<View>
      <Text>Monitor</Text>
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
