import React from "react";
import { SendAndArchive } from "./SendAndArchive";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SendAndArchive",
  component: SendAndArchive,
};

export const Default = () => <SendAndArchive />;
export const Fill = () => <SendAndArchive fill="blue" />;
export const Size = () => <SendAndArchive height="50" width="50" />;
export const CustomStyle = () => <SendAndArchive style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SendAndArchive className="custom-class" />;

export const Clickable = () => <SendAndArchive onClick={()=>console.log("clicked")} />;

const Template = (args) => <SendAndArchive {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
