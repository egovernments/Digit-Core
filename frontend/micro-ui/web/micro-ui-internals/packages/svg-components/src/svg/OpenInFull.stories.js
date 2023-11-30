import React from "react";
import { OpenInFull } from "./OpenInFull";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OpenInFull",
  component: OpenInFull,
};

export const Default = () => <OpenInFull />;
export const Fill = () => <OpenInFull fill="blue" />;
export const Size = () => <OpenInFull height="50" width="50" />;
export const CustomStyle = () => <OpenInFull style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OpenInFull className="custom-class" />;

export const Clickable = () => <OpenInFull onClick={()=>console.log("clicked")} />;

const Template = (args) => <OpenInFull {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
