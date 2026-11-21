import React from "react";
import { NoteAdd } from "./NoteAdd";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NoteAdd",
  component: NoteAdd,
};

export const Default = () => <NoteAdd />;
export const Fill = () => <NoteAdd fill="blue" />;
export const Size = () => <NoteAdd height="50" width="50" />;
export const CustomStyle = () => <NoteAdd style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoteAdd className="custom-class" />;

export const Clickable = () => <NoteAdd onClick={()=>console.log("clicked")} />;

const Template = (args) => <NoteAdd {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
