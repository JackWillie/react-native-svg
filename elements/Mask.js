import React, { Component } from "react";
import PropTypes from "prop-types";
import { requireNativeComponent } from "react-native";
import { numberProp } from "../lib/props";
import PATTERN_UNITS from "../lib/PATTERN_UNITS";
import { MaskAttributes } from "../lib/attributes";
import extractTransform from "../lib/extract/extractTransform";

export default class extends Component {
    static displayName = "Mask";
    static propTypes = {
        id: PropTypes.string.isRequired,
        x: numberProp,
        y: numberProp,
        width: numberProp,
        height: numberProp,
        maskTransform: PropTypes.string,
        maskUnits: PropTypes.oneOf(["userSpaceOnUse", "objectBoundingBox"]),
        maskContentUnits: PropTypes.oneOf([
            "userSpaceOnUse",
            "objectBoundingBox",
        ]),
    };

    setNativeProps = (props) => {
        if (props.width) {
            props.maskwidth = `${props.width}`;
        }
        if (props.height) {
            props.maskheight = `${props.height}`;
        }
        this.root.setNativeProps(props);
    };

    render() {
        const { props } = this;
        const {
            maskTransform,
            transform,
            id,
            x,
            y,
            width,
            height,
            maskUnits,
            maskContentUnits,
            children,
        } = props;

        let extractedTransform;
        if (maskTransform) {
            extractedTransform = extractTransform(maskTransform);
        } else if (transform) {
            extractedTransform = extractTransform(transform);
        } else {
            extractedTransform = extractTransform(props);
        }

        return (
            <RNSVGMask
                ref={ele => {
                    this.root = ele;
                }}
                name={id}
                x={`${x}`}
                y={`${y}`}
                maskwidth={`${width}`}
                maskheight={`${height}`}
                matrix={extractedTransform}
                maskTransform={extractedTransform}
                maskUnits={
                    maskUnits !== undefined ? PATTERN_UNITS[maskUnits] : 0
                }
                maskContentUnits={
                    maskContentUnits !== undefined
                        ? PATTERN_UNITS[maskContentUnits]
                        : 1
                }
            >
                {children}
            </RNSVGMask>
        );
    }
}

const RNSVGMask = requireNativeComponent("RNSVGMask", null, {
    nativeOnly: MaskAttributes,
});
