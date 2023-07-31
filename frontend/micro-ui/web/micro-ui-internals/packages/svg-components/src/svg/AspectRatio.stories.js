import React from "react";
import { AspectRatio } from "./AspectRatio";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AspectRatio",
  component: AspectRatio,
};

export const Default = () => <AspectRatio />;
export const Fill = () => <AspectRatio fill="blue" />;
export const Size = () => <AspectRatio height="50" width="50" />;
export const CustomStyle = () => <AspectRatio style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AspectRatio className="custom-class" />;
export const Clickable = () => <AspectRatio onClick={()=>console.log("clicked")} />;

const Template = (args) => <AspectRatio {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
