import React, {Component, PropTypes} from 'react';
import ViewBox from './ViewBox';
import G from './G';
import Defs from './Defs';

class SymbolElement extends Component{
    static displayName = 'Symbol';
    static propType = {
        id: PropTypes.string.isRequired,
        viewBox: PropTypes.string,
        preserveAspectRatio: PropTypes.string
    };
    render() {
        let {props} = this;

        let viewBox = props.viewBox;
        if (props.viewbox) {
            viewBox = props.viewbox;
            console.warn('Prop `viewbox` is deprecated. please use `viewBox` instead.');
        }

        let content = viewBox ? <ViewBox
            viewBox={viewBox}
            preserveAspectRatio={props.preserveAspectRatio}
        >
            {props.children}
        </ViewBox> : props.children;

        return <Defs>
            <G id={props.id}>
                {content}
            </G>
        </Defs>;
    }
}

export default SymbolElement;
