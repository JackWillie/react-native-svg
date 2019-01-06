import React from "react";
import Path from "./Path";
import Shape from "./Shape";
import extractPolyPoints from "../lib/extract/extractPolyPoints";

export default class extends Shape {
    static displayName = "Polyline";

    static defaultProps = {
        points: "",
    };

    setNativeProps = props => {
        const { points } = props;
        if (points) {
            props.d = `M${extractPolyPoints(points)}`;
        }
        this.root.setNativeProps(props);
    };

    render() {
        const { props } = this;
        const { points } = props;
        return (
            <Path
                ref={ele => {
                    this.root = ele;
                }}
                {...this.props}
                d={`M${extractPolyPoints(points)}`}
            />
        );
    }
}
