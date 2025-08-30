import React from "react";
import { ViewStream } from "./ViewStream";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ViewStream",
  component: ViewStream,
};

export const Default = () => <ViewStream />;
export const Fill = () => <ViewStream fill="blue" />;
export const Size = () => <ViewStream height="50" width="50" />;
export const CustomStyle = () => <ViewStream style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ViewStream className="custom-class" />;
export const Clickable = () => <ViewStream onClick={()=>console.log("clicked")} />;

const Template = (args) => <ViewStream {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
