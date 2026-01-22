import React from "react";
import { ZoomOutMap } from "./ZoomOutMap";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ZoomOutMap",
  component: ZoomOutMap,
};

export const Default = () => <ZoomOutMap />;
export const Fill = () => <ZoomOutMap fill="blue" />;
export const Size = () => <ZoomOutMap height="50" width="50" />;
export const CustomStyle = () => <ZoomOutMap style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ZoomOutMap className="custom-class" />;
export const Clickable = () => <ZoomOutMap onClick={()=>console.log("clicked")} />;

const Template = (args) => <ZoomOutMap {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};