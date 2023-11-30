import React from "react";
import { PublishedWithChanges } from "./PublishedWithChanges";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PublishedWithChanges",
  component: PublishedWithChanges,
};

export const Default = () => <PublishedWithChanges />;
export const Fill = () => <PublishedWithChanges fill="blue" />;
export const Size = () => <PublishedWithChanges height="50" width="50" />;
export const CustomStyle = () => <PublishedWithChanges style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PublishedWithChanges className="custom-class" />;

export const Clickable = () => <PublishedWithChanges onClick={()=>console.log("clicked")} />;

const Template = (args) => <PublishedWithChanges {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
