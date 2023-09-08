import React from "react";
import { CancelPresentation } from "./CancelPresentation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CancelPresentation",
  component: CancelPresentation,
};

export const Default = () => <CancelPresentation />;
export const Fill = () => <CancelPresentation fill="blue" />;
export const Size = () => <CancelPresentation height="50" width="50" />;
export const CustomStyle = () => <CancelPresentation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CancelPresentation className="custom-class" />;

export const Clickable = () => <CancelPresentation onClick={()=>console.log("clicked")} />;

const Template = (args) => <CancelPresentation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
