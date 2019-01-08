import React, { Component } from "react";
import { requireNativeComponent } from "react-native";
import extractGradient from "../lib/extract/extractGradient";

export default class RadialGradient extends Component {
    static displayName = "RadialGradient";

    static defaultProps = {
        fx: "50%",
        fy: "50%",
        cx: "50%",
        cy: "50%",
        r: "50%",
    };

    render() {
        const { props } = this;
        const { fx, fy, rx, ry, r, cx, cy } = props;
        return (
            <RNSVGRadialGradient
                ref={this.refMethod}
                fx={fx}
                fy={fy}
                rx={rx || r}
                ry={ry || r}
                cx={cx}
                cy={cy}
                {...extractGradient(props)}
            />
        );
    }
}

const RNSVGRadialGradient = requireNativeComponent("RNSVGRadialGradient");
