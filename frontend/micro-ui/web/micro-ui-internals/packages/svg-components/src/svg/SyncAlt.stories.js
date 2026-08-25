import React from "react";
import { SyncAlt } from "./SyncAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SyncAlt",
  component: SyncAlt,
};

export const Default = () => <SyncAlt />;
export const Fill = () => <SyncAlt fill="blue" />;
export const Size = () => <SyncAlt height="50" width="50" />;
export const CustomStyle = () => <SyncAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SyncAlt className="custom-class" />;
export const Clickable = () => <SyncAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <SyncAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
