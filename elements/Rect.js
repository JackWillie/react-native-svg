import React from "react";
import { requireNativeComponent } from "react-native";
import extractProps from "../lib/extract/extractProps";
import Shape from "./Shape";

export default class Rect extends Shape {
    static displayName = "Rect";

    static defaultProps = {
        x: 0,
        y: 0,
        width: 0,
        height: 0,
        rx: 0,
        ry: 0,
    };

    setNativeProps = props => {
        this.root.setNativeProps(props);
    };

    render() {
        const { props } = this;
        const { x, y, width, height, rx, ry } = props;
        return (
            <RNSVGRect
                ref={ele => {
                    this.root = ele;
                }}
                {...extractProps(
                    {
                        ...props,
                        x: null,
                        y: null,
                    },
                    this,
                )}
                x={x}
                y={y}
                width={width}
                height={height}
                rx={rx}
                ry={ry}
            />
        );
    }
}

const RNSVGRect = requireNativeComponent("RNSVGRect");
