import React from "react";
import { OpenInBrowser } from "./OpenInBrowser";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OpenInBrowser",
  component: OpenInBrowser,
};

export const Default = () => <OpenInBrowser />;
export const Fill = () => <OpenInBrowser fill="blue" />;
export const Size = () => <OpenInBrowser height="50" width="50" />;
export const CustomStyle = () => <OpenInBrowser style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OpenInBrowser className="custom-class" />;

export const Clickable = () => <OpenInBrowser onClick={()=>console.log("clicked")} />;

const Template = (args) => <OpenInBrowser {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
