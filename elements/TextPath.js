import React, {PropTypes} from 'react';
import createReactNativeComponentClass from 'react-native/Libraries/Renderer/src/renderers/native/createReactNativeComponentClass';
import {TextPathAttributes} from '../lib/attributes';
import extractText from '../lib/extract/extractText';
import Shape from './Shape';
import {pathProps, fontProps} from '../lib/props';
import TSpan from './TSpan';

const idExpReg = /^#(.+)$/;

class TextPath extends Shape {
    static displayName = 'Span';

    static propTypes = {
        ...pathProps,
        ...fontProps,
        href: PropTypes.string.isRequired,
        textAnchor: PropTypes.oneOf(['start', 'middle', 'end'])
    };

    render() {
        let {children, href, ...props} = this.props;
        if (href) {
            let matched = href.match(idExpReg);

            if (matched) {
                href = matched[1];

                return <RNSVGTextPath
                    href={href}
                    {...this.extractProps({
                        ...props,
                        x: null,
                        y: null
                    })}
                    {...extractText({children}, true)}
                />;
            }
        }

        console.warn('Invalid `href` prop for `TextPath` element, expected a href like `"#id"`, but got: "' + props.href + '"');
        return <TSpan>{children}</TSpan>
    }

}

const RNSVGTextPath = createReactNativeComponentClass({
    validAttributes: TextPathAttributes,
    uiViewClassName: 'RNSVGTextPath'
});

export default TextPath;
