import React from "react";
import { requireNativeComponent } from "react-native";
import extractText, { setTSpan } from "../lib/extract/extractText";
import extractProps from "../lib/extract/extractProps";
import extractTransform from "../lib/extract/extractTransform";
import Shape from "./Shape";
import { pickNotNil } from "../lib/util";

// TSpan elements are shadow components
export default class TSpan extends Shape {
    static displayName = "TSpan";

    setNativeProps = props => {
        const matrix = !props.matrix && extractTransform(props);
        if (matrix) {
            props.matrix = matrix;
        }
        const text = pickNotNil(extractText(props, false));
        this.root.setNativeProps({
            ...props,
            ...text,
        });
    };

    render() {
        const props = this.props;
        return (
            <RNSVGTSpan
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
                {...extractText(props, false)}
            />
        );
    }
}

setTSpan(TSpan);

const RNSVGTSpan = requireNativeComponent("RNSVGTSpan");
