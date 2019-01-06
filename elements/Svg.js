//noinspection JSUnresolvedVariable
import React from "react";
import {
    requireNativeComponent,
    StyleSheet,
    findNodeHandle,
    NativeModules,
} from "react-native";
import extractResponder from "../lib/extract/extractResponder";
import extractViewBox from "../lib/extract/extractViewBox";
import Shape from "./Shape";
import G from "./G";

/** @namespace NativeModules.RNSVGSvgViewManager */
const RNSVGSvgViewManager = NativeModules.RNSVGSvgViewManager;

// Svg - Root node of all Svg elements
let id = 0;

const styles = StyleSheet.create({
    svg: {
        backgroundColor: "transparent",
        borderWidth: 0,
    },
});

class Svg extends Shape {
    static displayName = "Svg";

    static defaultProps = {
        preserveAspectRatio: "xMidYMid meet",
    };

    constructor() {
        super(...arguments);
        this.id = ++id;
    }
    measureInWindow = (...args) => {
        this.root.measureInWindow(...args);
    };

    measure = (...args) => {
        this.root.measure(...args);
    };

    measureLayout = (...args) => {
        this.root.measureLayout(...args);
    };

    setNativeProps = props => {
        const { width, height } = props;
        if (width) {
            props.bbWidth = `${width}`;
        }
        if (height) {
            props.bbHeight = `${height}`;
        }
        this.root.setNativeProps(props);
    };

    toDataURL = callback => {
        callback &&
            RNSVGSvgViewManager.toDataURL(findNodeHandle(this.root), callback);
    };

    render() {
        const {
            opacity,
            viewBox,
            preserveAspectRatio,
            style,
            children,
            onLayout,
            ...props
        } = this.props;
        const stylesAndProps = {
            ...(style && style.length ? Object.assign({}, ...style) : style),
            ...props,
        };
        const {
            color,
            width,
            height,

            // Inherited G properties
            font,
            transform,
            fill,
            fillOpacity,
            fillRule,
            stroke,
            strokeWidth,
            strokeOpacity,
            strokeDasharray,
            strokeDashoffset,
            strokeLinecap,
            strokeLinejoin,
            strokeMiterlimit,
        } = stylesAndProps;

        const dimensions = width && height ? {
            width: width[width.length - 1] === "%" ? width : +width,
            height: height[height.length - 1] === "%" ? height : +height,
            flex: 0,
        } : null;

        const w = `${width}`;
        const h = `${height}`;

        return (
            <NativeSvgView
                {...props}
                bbWidth={w}
                bbHeight={h}
                tintColor={color}
                onLayout={onLayout}
                {...extractResponder(props, this)}
                {...extractViewBox({ viewBox, preserveAspectRatio })}
                ref={ele => {
                    this.root = ele;
                }}
                style={[
                    styles.svg,
                    style,
                    !isNaN(+opacity) && {
                        opacity: +opacity,
                    },
                    dimensions,
                ]}
            >
                <G
                    style={style}
                    {...{
                        font,
                        transform,
                        fill,
                        fillOpacity,
                        fillRule,
                        stroke,
                        strokeWidth,
                        strokeOpacity,
                        strokeDasharray,
                        strokeDashoffset,
                        strokeLinecap,
                        strokeLinejoin,
                        strokeMiterlimit,
                    }}
                >
                    {children}
                </G>
            </NativeSvgView>
        );
    }
}

const NativeSvgView = requireNativeComponent("RNSVGSvgView");

export default Svg;
